# 📚 دراستي (Drasaty) — تطبيق الدراسة الشامل

تطبيق Android مصمم خصيصاً للطلاب العرب. يساعدك على تنظيم جدولك الدراسي، تتبع مهامك، والمذاكرة بتقنية البومودورو.

---

## ✨ الميزات

- 📅 **جدول أسبوعي** — أضف موادك لكل يوم (السبت → الجمعة)
- ✅ **إدارة المهام** — أضف مهامك مع تحديد المادة، التاريخ، والأولوية
- 🔔 **إشعارات ذكية** — تذكير قبل الحصة بـ 10 دقايق، وقبل أي مهمة
- ⏱️ **بومودورو تايمر** — 25 دقيقة مذاكرة / 5 دقايق راحة
- 📊 **إحصائيات** — تابع إنجازك وتقدمك
- 🌙 **إعادة جدولة المنبهات** — بعد إعادة تشغيل الجهاز
- 🇸🇦 **دعم كامل للعربية** — RTL، أيام الأسبوع بالعربي

---

## 🛠️ التقنيات المستخدمة

- **اللغة:** Kotlin
- **قاعدة البيانات:** Room
- **UI:** Material Components, ConstraintLayout, RecyclerView
- **الإشعارات:** NotificationManager + AlarmManager
- **Async:** Kotlin Coroutines
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

---

## 🚀 خطوات بناء الـ APK

### الطريقة 1: بـ Android Studio (الأسهل) ⭐

#### 1. تنزيل وتثبيت Android Studio

- من: https://developer.android.com/studio
- اختر نظامك (Windows/Mac/Linux)
- ثبّت البرنامج واتبع خطوات الـ Setup Wizard

#### 2. تنزيل Gradle wrapper

المشروع يحتاج ملف `gradle-wrapper.jar`. Android Studio عادةً بينزله تلقائياً عند أول Sync، لكن لو مش موجود:

- افتح Android Studio
- اذهب لـ: `File > Settings > Build, Execution, Deployment > Gradle`
- اضغط على زر "..." جنب "Use Gradle from"
- أو استخدم Gradle المُحمّل من هنا:
  https://services.gradle.org/distributions/gradle-8.4-bin.zip

#### 3. فتح المشروع

1. افتح Android Studio
2. اختار: `File > Open`
3. اختار مجلد المشروع: `DrasatyApp/`
4. انتظر Gradle Sync (ممكن ياخد دقيقة أو اتنين)

#### 4. بناء APK

1. من القائمة: `Build > Build Bundle(s) / APK(s) > Build APK(s)`
2. انتظر حتى يخلص البناء
3. لما يطلع Dialog "APK(s) generated successfully"
4. اضغط "locate" — هتلاقي الـ APK في:
   ```
   DrasatyApp/app/build/outputs/apk/debug/app-debug.apk
   ```

#### 5. تثبيت الـ APK على موبايلك

**الطريقة 1:** وصّل الموبايل بالـ USB (فعّل USB Debugging من خيارات المطور) — Android Studio هيعرضلك زرار "Run" أخضر، اضغطه.

**الطريقة 2:** انقل ملف `app-debug.apk` للموبايل، اضغط عليه، وفعّل "تثبيت من مصادر غير معروفة" لما يطلب.

---

### الطريقة 2: بـ Command Line (Gradle مباشرة)

#### المتطلبات
- JDK 17
- Android SDK (يُحمّل مع Android Studio)

#### الخطوات

```bash
cd DrasatyApp

# تأكد إن JAVA_HOME مضبوط
export JAVA_HOME=/path/to/jdk-17

# بناء Debug APK
./gradlew assembleDebug

# الـ APK هيتولد في:
# app/build/outputs/apk/debug/app-debug.apk
```

#### بناء Release APK (موقّع)

```bash
# 1. أنشئ keystore
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key

# 2. أضف keystore config في app/build.gradle
# 3. شغّل:
./gradlew assembleRelease
```

---

## 📂 هيكل المشروع

```
DrasatyApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/drasaty/app/
│   │   │   ├── activities/
│   │   │   │   ├── MainActivity.kt         # النشاط الرئيسي
│   │   │   │   ├── HomeFragment.kt         # شاشة الرئيسية
│   │   │   │   ├── ScheduleFragment.kt     # شاشة الجدول
│   │   │   │   ├── TasksFragment.kt        # شاشة المهام
│   │   │   │   ├── PomodoroFragment.kt     # شاشة البومودورو
│   │   │   │   └── SettingsFragment.kt     # شاشة الإعدادات
│   │   │   ├── db/
│   │   │   │   ├── Entities.kt             # كيانات قاعدة البيانات
│   │   │   │   ├── Daos.kt                 # استعلامات قاعدة البيانات
│   │   │   │   └── Database.kt             # قاعدة بيانات Room
│   │   │   ├── adapters/
│   │   │   │   ├── ScheduleAdapter.kt      # أدابتر الجدول
│   │   │   │   └── TaskAdapter.kt          # أدابتر المهام
│   │   │   ├── notifications/
│   │   │   │   ├── NotificationsHelper.kt  # منطق الإشعارات
│   │   │   │   ├── ReminderReceiver.kt     # مستلم التنبيهات
│   │   │   │   └── BootReceiver.kt         # إعادة الجدولة بعد reboot
│   │   │   └── Utils.kt                    # أدوات مساعدة
│   │   ├── res/                            # ملفات الواجهة (XML)
│   │   └── AndroidManifest.xml             # إعدادات التطبيق
│   ├── build.gradle                        # Gradle script للتطبيق
│   └── proguard-rules.pro
├── build.gradle                            # Gradle script للمشروع
├── settings.gradle
├── gradle.properties
└── README.md
```

---

## 🐛 حل المشاكل الشائعة

### 1. خطأ "Gradle sync failed"

**الحل:**
- تأكد إن JDK 17 مثبت
- `File > Invalidate Caches / Restart` من Android Studio
- تأكد من الاتصال بالإنترنت (Gradle بينزل dependencies من Maven Central)

### 2. خطأ "Cannot find symbol" في Kotlin

**الحل:**
- اضغط على الـ Build مرتين (Sync + Build)
- `File > Sync with File System`

### 3. الإشعارات مش بتيجي

**الحل:**
- روح لـ: `Settings > Apps > دراستي > Notifications` وفعّلها
- على Android 12+: `Settings > Apps > دراستي > Alarms & reminders` وفعّل "Allow setting alarms and reminders"

### 4. الـ APK مش بيتثبت

**الحل:**
- فعّل "Install from unknown sources" من إعدادات الأمان
- تأكد إن الـ APK مش متضرر (حجمه الطبيعي ~5-10 MB)

---

## 📝 إضافة بيانات تجريبية

بعد ما تفتح التطبيق، روح لـ "الجدول الأسبوعي" واضغط (+) على أي يوم لإضافة حصة. وللمهام اضغط (+) في شاشة المهام.

**مثال على جدول:**
```
السبت:
  - رياضيات | 08:00 - 09:30 | قاعة 101
  - فيزياء  | 10:00 - 11:30 | قاعة 203
  - عربي    | 12:00 - 13:30 | قاعة 105
```

---

## 📜 الترخيص

هذا المشروع مفتوح المصدر — استخدمه وطوره براحتك.

---

## 💬 تواصل

لو عندك أي مشكلة في البناء أو سؤال، ابعتلي رسالة!

صُنع بـ ❤️ للطلاب العرب