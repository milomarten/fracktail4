package com.github.milomarten.fracktail.core.birthday.v3;

import java.time.LocalDate;

public record EventAndWhen<T>(T event, LocalDate when) {
}
