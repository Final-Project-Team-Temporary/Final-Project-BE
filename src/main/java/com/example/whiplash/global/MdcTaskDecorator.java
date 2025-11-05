package com.example.whiplash.global;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import java.util.Map;

/**
 * MDC(Mapped Diagnostic Context)를 비동기 스레드로 전파
 */
public class MdcTaskDecorator implements TaskDecorator {

	@Override
	public Runnable decorate(Runnable runnable) {
		// 현재 스레드의 MDC 복사
		Map<String, String> contextMap = MDC.getCopyOfContextMap();

		return () -> {
			try {
				// 새 스레드에 MDC 설정
				if (contextMap != null) {
					MDC.setContextMap(contextMap);
				}
				runnable.run();
			} finally {
				// 실행 후 MDC 정리
				MDC.clear();
			}
		};
	}
}
