package com.example.shechaniahinformationsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class HashMap implements Map<String, String> {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        return false;
    }

    @Override
    public boolean containsValue(@Nullable Object o) {
        return false;
    }

    @Nullable
    @Override
    public String get(@Nullable Object o) {
        return "";
    }

    @Nullable
    @Override
    public String put(String s, String s2) {
        return "";
    }

    @Nullable
    @Override
    public String remove(@Nullable Object o) {
        return "";
    }

    @Override
    public void putAll(@NonNull Map<? extends String, ? extends String> map) {

    }

    @Override
    public void clear() {

    }

    @NonNull
    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }

    @NonNull
    @Override
    public Collection<String> values() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return Collections.emptySet();
    }
}
