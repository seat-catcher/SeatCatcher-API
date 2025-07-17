package com.sullung2yo.seatcatcher.common.utility;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ScrollPaginationCollection<T> {

    private final List<T> itemsWithNextCursor;
    private final int countPerScroll;

    public static <T> ScrollPaginationCollection<T> of(List<T> itemsWithNextCursor, int size) {
        return new ScrollPaginationCollection<>(itemsWithNextCursor, size);
    }

    public boolean isLastScroll() {
        return this.itemsWithNextCursor.size() <= countPerScroll;
    }

    public List<T> getCurrentScrollItems() {
        if (isLastScroll()) {
            return this.itemsWithNextCursor;
        }
        return this.itemsWithNextCursor.subList(0, countPerScroll);
    }

    public T getNextCursor() {
        if (isLastScroll()) {
            return null; // 더 이상 다음 커서가 없을 경우 null 반환
        }
        return itemsWithNextCursor.get(countPerScroll - 1);
    }

}