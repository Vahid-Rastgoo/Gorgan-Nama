package com.vahid.gorgannama.data

data class Category(val id: String, val title: String, val short: String)

object Categories {
    val all = listOf(
        Category("restaurant", "رستوران و فست‌فود", "رستوران"),
        Category("cafe", "کافه و شیرینی", "کافه"),
        Category("grocery", "سوپرمارکت و پروتئین", "سوپرمارکت"),
        Category("clothing", "پوشاک، کیف و کفش", "پوشاک"),
        Category("beauty", "آرایشگاه و زیبایی", "زیبایی"),
        Category("medical", "پزشکی و داروخانه", "سلامت"),
        Category("auto", "خودرو و مکانیکی", "خودرو"),
        Category("home", "لوازم خانگی و ساختمانی", "خانه"),
        Category("tech", "موبایل و کامپیوتر", "دیجیتال"),
        Category("education", "آموزشگاه و کلاس", "آموزش"),
        Category("services", "خدمات و تعمیرات", "خدمات"),
        Category("sport", "ورزش و تفریح", "ورزش"),
        Category("tourism", "اقامتگاه و گردشگری", "گردشگری"),
        Category("crafts", "صنایع‌دستی و محصولات محلی", "محلی"),
        Category("other", "سایر", "سایر")
    )

    fun title(id: String): String =
        if (id.isBlank()) "" else all.firstOrNull { it.id == id }?.title ?: "سایر"
}

object Neighborhoods {
    val DEFAULT = listOf(
        "ولیعصر", "عدالت", "ناهارخوران", "شالیکوبی", "گرگانپارس", "گلشهر",
        "بلوار جرجان", "بلوار کاشانی", "پنجم آذر", "رسالت", "ایرانمهر",
        "زیباشهر", "کوی انتظام", "میدان مازندران", "نعلبندان", "بازار"
    )
}
