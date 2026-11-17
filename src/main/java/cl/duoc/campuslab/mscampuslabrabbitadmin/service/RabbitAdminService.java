
package cl.duoc.campuslab.mscampuslabrabbitadmin.service;

import cl.duoc.campuslab.mscampuslabrabbitadmin.dto.BindingRequest;
import cl.duoc.campuslab.mscampuslabrabbitadmin.dto.ExchangeRequest;
import cl.duoc.campuslab.mscampuslabrabbitadmin.dto.QueueRequest;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    // Componentes principales de CampusLab.
    // No deben modificarse desde el panel administrativo.
    private static final Set<String> PROTECTED_QUEUES = Set.of(
            "q.cmd.email",
            "q.cmd.prep",
            "q.cmd.voucher",
            "q.cmd.email.dlq",
            "q.cmd.prep.dlq",
            "q.cmd.voucher.dlq"
    );

    private static final Set<String> PROTECTED_EXCHANGES = Set.of(
            "cmd.direct",
            "cmd.topic",
            "cmd.dead.dlx"
    );

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    // ==========================================
    // 1. CREAR COLA
    // ==========================================

    public void createQueue(QueueRequest request) {

        String name = validateName(request.getName());

        validateQueueNotProtected(name);

        Queue queue;

        if (request.isDurable()) {
            queue = QueueBuilder
                    .durable(name)
                    .build();
        } else {
            queue = QueueBuilder
                    .nonDurable(name)
                    .build();
        }

        rabbitAdmin.declareQueue(queue);
    }

    // ==========================================
    // 2. ELIMINAR COLA
    // ==========================================

    public boolean deleteQueue(String name) {

        name = validateName(name);

        validateQueueNotProtected(name);

        return rabbitAdmin.deleteQueue(name);
    }

    // ==========================================
    // 3. CREAR EXCHANGE
    // ==========================================

    public void createExchange(ExchangeRequest request) {

        String name = validateName(request.getName());

        validateExchangeNotProtected(name);

        Exchange exchange = buildExchange(
                name,
                request.getType()
        );

        rabbitAdmin.declareExchange(exchange);
    }

    // ==========================================
    // 4. ELIMINAR EXCHANGE
    // ==========================================

    public boolean deleteExchange(String name) {

        name = validateName(name);

        validateExchangeNotProtected(name);

        return rabbitAdmin.deleteExchange(name);
    }

    // ==========================================
    // 5. CREAR BINDING
    // ==========================================

    public void createBinding(BindingRequest request) {

        validateBindingComponents(request);

        Queue queue = new Queue(
                request.getQueueName().trim(),
                true
        );

        Binding binding = buildBinding(
                queue,
                request.getExchangeName().trim(),
                request.getExchangeType(),
                request.getRoutingKey()
        );

        rabbitAdmin.declareBinding(binding);
    }

    // ==========================================
    // 6. ELIMINAR BINDING
    // ==========================================

    public void deleteBinding(BindingRequest request) {

        validateBindingComponents(request);

        Queue queue = new Queue(
                request.getQueueName().trim(),
                true
        );

        Binding binding = buildBinding(
                queue,
                request.getExchangeName().trim(),
                request.getExchangeType(),
                request.getRoutingKey()
        );

        rabbitAdmin.removeBinding(binding);
    }

    // ==========================================
    // 7. PROTEGER COMPONENTES CRÍTICOS
    // ==========================================

    private void validateQueueNotProtected(String name) {

        if (PROTECTED_QUEUES.contains(name)
                || name.startsWith("q.cmd.")
                || name.startsWith("amq.")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se permite modificar esta cola protegida: "
                            + name
            );
        }
    }

    private void validateExchangeNotProtected(String name) {

        if (PROTECTED_EXCHANGES.contains(name)
                || name.startsWith("cmd.")
                || name.startsWith("amq.")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se permite modificar este exchange protegido: "
                            + name
            );
        }
    }

    private void validateBindingComponents(
            BindingRequest request
    ) {

        String queueName = validateName(
                request.getQueueName()
        );

        String exchangeName = validateName(
                request.getExchangeName()
        );

        validateQueueNotProtected(queueName);
        validateExchangeNotProtected(exchangeName);
    }

    private String validateName(String name) {

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del componente es obligatorio"
            );
        }

        return name.trim();
    }

    // ==========================================
    // 8. CONSTRUIR EXCHANGE
    // ==========================================

    private Exchange buildExchange(
            String exchangeName,
            String exchangeType
    ) {

        if (exchangeType == null
                || exchangeType.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo de exchange es obligatorio"
            );
        }

        return switch (
                exchangeType.toLowerCase(Locale.ROOT)
                        .trim()
                ) {

            case "direct" -> new DirectExchange(
                    exchangeName,
                    true,
                    false
            );

            case "topic" -> new TopicExchange(
                    exchangeName,
                    true,
                    false
            );

            case "fanout" -> new FanoutExchange(
                    exchangeName,
                    true,
                    false
            );

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de exchange no soportado"
            );
        };
    }

    // ==========================================
    // 9. CONSTRUIR BINDING
    // ==========================================

    private Binding buildBinding(
            Queue queue,
            String exchangeName,
            String exchangeType,
            String routingKey
    ) {

        if (exchangeType == null
                || exchangeType.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo de exchange es obligatorio"
            );
        }

        return switch (
                exchangeType.toLowerCase(Locale.ROOT)
                        .trim()
                ) {

            case "direct" -> {

                validateRoutingKey(routingKey);

                DirectExchange exchange =
                        new DirectExchange(
                                exchangeName,
                                true,
                                false
                        );

                yield BindingBuilder
                        .bind(queue)
                        .to(exchange)
                        .with(routingKey.trim());
            }

            case "topic" -> {

                validateRoutingKey(routingKey);

                TopicExchange exchange =
                        new TopicExchange(
                                exchangeName,
                                true,
                                false
                        );

                yield BindingBuilder
                        .bind(queue)
                        .to(exchange)
                        .with(routingKey.trim());
            }

            case "fanout" -> {

                FanoutExchange exchange =
                        new FanoutExchange(
                                exchangeName,
                                true,
                                false
                        );

                yield BindingBuilder
                        .bind(queue)
                        .to(exchange);
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de exchange no soportado"
            );
        };
    }

    // ==========================================
    // 10. VALIDAR ROUTING KEY
    // ==========================================

    private void validateRoutingKey(String routingKey) {

        if (routingKey == null
                || routingKey.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La routing key es obligatoria para direct y topic"
            );
        }
    }
}
