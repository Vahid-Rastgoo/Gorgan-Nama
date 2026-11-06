package com.vahid.gorgannama

/**
 * تنظیمات زمان ساخت.
 * نسخه شخصی (برای خودت): همین‌طور بماند.
 * نسخه عمومی (برای مردم): PUBLIC_BUILD را true کن و لینک کاتالوگ و شماره‌ات را بنویس، بعد APK بگیر.
 */
object Config {
    /** true = تب مدیریت مخفی می‌شود (با ۵ ضربه روی عنوان باز می‌شود) و داده‌های نمونه بار نمی‌شوند */
    const val PUBLIC_BUILD = false

    /** لینک مستقیم فایل کاتالوگ منتشرشده، مثل https://example.com/gorgan-catalog.json */
    const val DEFAULT_CATALOG_URL = ""

    /** شماره خودت برای دکمه‌های «ثبت کسب‌وکار من» و «گزارش اشتباه» */
    const val ADMIN_PHONE = ""
}
