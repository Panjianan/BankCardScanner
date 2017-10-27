# BankCardScanner
扫描银行卡号
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)[ ![Download](https://api.bintray.com/packages/tsubasap91/maven/bankcardscanner/images/download.svg) ](https://bintray.com/tsubasap91/maven/bankcardscanner/_latestVersion)

![image.png](http://upload-images.jianshu.io/upload_images/1712960-5a34eb0668762196.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](http://upload-images.jianshu.io/upload_images/1712960-6db43a009eb2b079.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# How do I use it?

## Step 1

#### Gradle
```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.tsubasa:bankcardscanner:1.0.0'
}
```

## Step 2
start the scanner activity

```java
Intent intent = new Intent(MainActivity.this, ScanCamera.class);
startActivityForResult(intent, requestCode);
```

an then
```
 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCode && resultCode == RESULT_OK) {
            String result = data.getStringExtra(ScanCameraKt.EXTRA_SCAN_CARD_RESULT_STR);
            textView.setText(result);
        }
    }
```
