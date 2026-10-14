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

        Exchange exchange;

        switch (request.getType().toLowerCase()) {

            case "direct" -> exchange =
                    new DirectExchange(
                            request.getName(),
                            true,
                            false
                    );

            case "topic" -> exchange =
                    new TopicExchange(
                            request.getName(),
                            true,
                            false
                    );

            case "fanout" -> exchange =
                    new FanoutExchange(
                            request.getName(),
                            true,
                            false
                    );

            default -> throw new IllegalArgumentException(
                    "Tipo de exchange no soportado. Use direct, topic o fanout"
            );
        }

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

        DirectExchange exchange = new DirectExchange(
                request.getExchangeName(),
                true,
                false
        );

        Binding binding = BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(request.getRoutingKey());

        rabbitAdmin.declareBinding(binding);
    }

    public void deleteBinding(BindingRequest request) {

        Queue queue = new Queue(
                request.getQueueName(),
                true
        );

        DirectExchange exchange = new DirectExchange(
                request.getExchangeName(),
                true,
                false
        );

        Binding binding = BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(request.getRoutingKey());

        rabbitAdmin.removeBinding(binding);
    }
}