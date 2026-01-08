# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Firebase classes
-keepattributes Signature
-keepattributes *Annotation*

# Keep Room entities
-keep class com.studenthub.data.local.entity.** { *; }

# Keep Firestore model classes
-keep class com.studenthub.model.** { *; }
