package kr.rtustudio.bridge.proxium.api.protocol.internal;

/**
 * Sent before a graceful shutdown to notify the other side not to attempt reconnection.
 *
 * <p>정상 종료 직전에 전송하여 상대 측에서 재연결을 시도하지 않도록 알린다.
 */
public record Disconnect() {}
