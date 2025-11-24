package com.example.whiplash.global.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher{
	private final ApplicationEventPublisher publisher;

	@Override
	public void publish(Object event) {
		publisher.publishEvent(event);
	}
}
