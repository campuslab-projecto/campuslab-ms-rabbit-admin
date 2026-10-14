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
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public void createQueue(QueueRequest request) {

        Queue queue;

        if (request.isDurable()) {
            queue = QueueBuilder
                    .durable(request.getName())
                    .build();
        } else {
            queue = QueueBuilder
                    .nonDurable(request.getName())
                    .build();
        }

        rabbitAdmin.declareQueue(queue);
    }

    public boolean deleteQueue(String name) {
        return rabbitAdmin.deleteQueue(name);
    }

    public void createExchange(ExchangeRequest request) {

        Exchange exchange = buildExchange(
                request.getName(),
                request.getType()
        );

        rabbitAdmin.declareExchange(exchange);
    }

    public boolean deleteExchange(String name) {
        return rabbitAdmin.deleteExchange(name);
    }

    public void createBinding(BindingRequest request) {

        Queue queue = new Queue(
                request.getQueueName(),
                true
        );

        Binding binding = buildBinding(
                queue,
                request.getExchangeName(),
                request.getExchangeType(),
                request.getRoutingKey()
        );

        rabbitAdmin.declareBinding(binding);
    }

    public void deleteBinding(BindingRequest request) {

        Queue queue = new Queue(
                request.getQueueName(),
                true
        );

        Binding binding = buildBinding(
                queue,
                request.getExchangeName(),
                request.getExchangeType(),
                request.getRoutingKey()
        );

        rabbitAdmin.removeBinding(binding);
    }

    private Exchange buildExchange(
            String exchangeName,
            String exchangeType
    ) {

        if (exchangeType == null || exchangeType.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de exchange es obligatorio"
            );
        }

        return switch (exchangeType.toLowerCase()) {

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

            default -> throw new IllegalArgumentException(
                    "Tipo de exchange no soportado. Use direct, topic o fanout"
            );
        };
    }

    private Binding buildBinding(
            Queue queue,
            String exchangeName,
            String exchangeType,
            String routingKey
    ) {

        if (exchangeType == null || exchangeType.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de exchange es obligatorio"
            );
        }

        return switch (exchangeType.toLowerCase()) {

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
                        .with(routingKey);
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
                        .with(routingKey);
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

            default -> throw new IllegalArgumentException(
                    "Tipo de exchange no soportado. Use direct, topic o fanout"
            );
        };
    }

    private void validateRoutingKey(String routingKey) {

        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException(
                    "La routing key es obligatoria para exchanges direct y topic"
            );
        }
    }
}