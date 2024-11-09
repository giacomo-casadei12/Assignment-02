package sap.ass02.userservice.utils;

import java.util.Objects;

public record Triple<T, U, V>(T first, U second, V third) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return Objects.equals(first, triple.first) && Objects.equals(third, triple.third) && Objects.equals(second, triple.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
