package ru.practicum.shareit.validation;

public final class ValidationKeys {
    private ValidationKeys() {
    }

    // USER
    public static final String USER_EMAIL_NOT_BLANK = "{user.email.notBlank}";
    public static final String USER_EMAIL_INVALID = "{user.email.invalid}";
    public static final String USER_ID_NOT_NULL = "{user.id.notNull}";

    // ITEM
    public static final String ITEM_NAME_NOT_BLANK = "{item.name.notBlank}";
    public static final String ITEM_AVAILABLE_NOT_BLANK = "{item.available.notBlank}";
    public static final String ITEM_DESCRIPTION_NOT_BLANK = "{item.description.notBlank}";
    public static final String ITEM_ID_NOT_NULL = "{item.id.notNull}";

    // BOOKING
    public static final String BOOKING_ID_NOT_NULL = "{booking.id.notNull}";
    public static final String BOOKING_START_NOT_NULL = "{booking.start.notNull}";
    public static final String BOOKING_END_NOT_NULL = "{booking.end.notNull}";
    public static final String BOOKING_ITEM_NOT_NULL = "{booking.item.notNull}";
    public static final String BOOKING_BOOKER_NOT_NULL = "{booking.booker.notNull}";

    // REQUEST
    public static final String ITEM_REQUEST_ID_NOT_NULL = "{item.request.id.notNull}";
    public static final String ITEM_REQUEST_DESCRIPTION_NOT_BLANK = "{item.request.description.notBlank}";
    public static final String ITEM_REQUEST_REQUESTOR_NOT_NULL = "{item.request.requestor.notNull}";
    public static final String ITEM_REQUEST_CREATED_NOT_NULL = "{item.request.created.notNull}";
}