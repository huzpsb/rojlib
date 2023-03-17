package roj.net.http.srv;

import roj.NotThreadSafe;
import roj.net.http.IllegalRequestException;

import javax.annotation.Nullable;

/**
 * @author Roj234
 * @since 2020/11/28 20:54
 */
@FunctionalInterface
public interface Router {
	// 1MB
	int DEFAULT_POST_SIZE = 1048576;

	default int writeTimeout(@Nullable Request req, @Nullable Response resp) {
		return 2000;
	}
	default int readTimeout() {
		return 5000;
	}

	Response response(@NotThreadSafe Request req, ResponseHeader rh) throws Exception;

	default void checkHeader(Request req, @Nullable PostSetting cfg) throws IllegalRequestException {}

	default int keepaliveTimeout() {
		return 300_000;
	}
	default int maxHeaderSize() {
		return 8192;
	}
}
