package com.github.milomarten.fracktail4.base.subcommand;

import com.github.milomarten.fracktail4.base.parameter.Parameters;
import com.github.milomarten.fracktail4.base.parameter.DefaultParameterParser;
import com.github.milomarten.fracktail4.base.parameter.ParameterParser;
import com.github.milomarten.fracktail4.base.parameter.SubcommandParameterParser;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class Router<CONTEXT> implements Handler<CONTEXT> {
    private final String name;
    private final Map<String, ParameterizedHandler<CONTEXT>> handlers = new HashMap<>();

    public Router<CONTEXT> subroute(String parameter, Router<CONTEXT> subrouter) {
        this.handlers.put(parameter, new ParameterizedHandler<>(subrouter, subrouter.getParameterParser()));
        return this;
    }

    public Router<CONTEXT> handle(String parameter, Handler<CONTEXT> handler, ParameterParser parser) {
        this.handlers.put(parameter, new ParameterizedHandler<>(handler, parser));
        return this;
    }

    public Router<CONTEXT> handle(String parameter, Handler<CONTEXT> handler) {
        return handle(parameter, handler, DefaultParameterParser.INSTANCE);
    }

    @Override
    public Mono<?> handle(Parameters parameters, CONTEXT context) {
        return Mono.defer(() -> {
            Optional<String> param = parameters.getParameter(0);
            if (param.isPresent()) {
                var handler = handlers.get(param.get());
                if (handler == null) {
                    return Mono.error(new UnknownCommandException(param.get()));
                }
                return handler.handle(parameters.range(1), context);
            } else {
                return Mono.error(new UnknownCommandException(""));
            }
        });
    }

    public ParameterParser getParameterParser() {
        Map<String, ParameterParser> parserMap = new HashMap<>(handlers.size());
        for (var entryset : handlers.entrySet()) {
            parserMap.put(
                    entryset.getKey(),
                    entryset.getValue().parser()
            );
        }
        return SubcommandParameterParser.builder()
                .options(parserMap)
                .build();
    }
}
