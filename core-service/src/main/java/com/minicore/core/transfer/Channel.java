package com.minicore.core.transfer;

/** Kênh phát sinh giao dịch, khớp CHECK constraint của cột TXN.CHANNEL. */
public enum Channel {
    TELLER,
    DIGITAL,
    PARTNER
}
