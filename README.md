Document：[Android App Bundles](https://developer.android.com/guide/app-bundle/)

Google sample in Kotlin: [https://github.com/googlesamples/android-dynamic-features](https://github.com/googlesamples/android-dynamic-features).

# Summary

It is risky to use Dynamic Delivery now because it is in beta 

![1532603583978.png](https://i.loli.net/2018/07/27/5b5ab9bec271b.png)

## Problems

1. ClassNotFoundException. This can be solved by adding `split="modulename"` attribute in manifest file. 

2. ResourceNotFound. This error will happen on API 26 or higher using 

	```java
	api 'com.google.android.play:core:1.3.0'
	```

	**Details**

	After first installing the module, you should restart the app. 

	> Reference: [https://developer.android.com/guide/app-bundle/playcore#access_downloaded_modules](https://developer.android.com/guide/app-bundle/playcore#access_downloaded_modules)

	Before restarting the app, starting activity of the module will result in ResourceNotFound Exception on all API level. The stack trace is same with following stack trace. But if we restart the app, ResourceNotFound Exception will still happen on API 26 or higher and the module will work on API 25 or lower.

	**Root Cause**

	Just guess. 

	- On phones with API 26 or higher, the base.apk is stored in data/app/packageName + Random-hashcode.
	- On phones with API 25 or lower, the base.apk is stored in data/app/packageName-1 or 2, etc.

	I guess the difference may be here.

3. getResource.getIdentifier() may not work in submodule. I think it is due to resource not in context. 

4. Be careful to manage resource name for avoiding resource name conflict, you can add prefix limit in build.gradle.

## Others

1. When users first download the app, they will only download the base apk (base module). After users downloading dynamic feature modules, the play store will remember which modules have been downloaded. Hence, if users uninstall the app and redownload the app, they will download all modules which they have downloaded before (make sense but not convenient for testing). 

  **Clear all data** of Play Store probably results in only downloading base app module next time.

2. We should restart app to use module. Details see [https://developer.android.com/guide/app-bundle/playcore#access_downloaded_modules](https://developer.android.com/guide/app-bundle/playcore#access_downloaded_modules)We 

3. The app base.apk is in `data/app/packageName`

  Downloaded split-feature.apk is in `data/user/0/packageName/files/splitcompat/versionCode/verifiled-splits`

  ![1532666235048.png](https://i.loli.net/2018/07/27/5b5ab7670f371.png)

