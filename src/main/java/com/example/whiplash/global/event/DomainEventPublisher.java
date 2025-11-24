package com.example.whiplash.global.event;

public interface DomainEventPublisher {
	void publish(Object event);
}
