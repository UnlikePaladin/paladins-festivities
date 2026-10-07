package com.unlikepaladin.pfest.registry;

public interface TriFunc<A, B, C, R> {
    R apply(A a, B b, C c);
}