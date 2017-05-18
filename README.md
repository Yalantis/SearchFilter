# SearchFilter

[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)]()
[![](https://jitpack.io/v/yalantis/searchfilter.svg)](https://jitpack.io/#yalantis/searchfilter)
[![Yalantis](https://raw.githubusercontent.com/Yalantis/PullToRefresh/develop/PullToRefreshDemo/Resources/badge_dark.png)](https://yalantis.com/?utm_source=github)

<a href='https://play.google.com/store/apps/details?id=com.yalantis.fitfilter&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="70" width="180"/></a>

[Live DEMO on appetize.io](https://appetize.io/app/3jke81pv6zuv9b4mt4gc53afc4)

Check this [project on dribbble](https://dribbble.com/shots/2818273-Female-in-IT-Filters)

Read how we did it [on our blog](https://yalantis.com/blog/develop-filter-animation-kotlin-android/)

<img src="gif/dribbble.gif"/>

##Requirements
- Android SDK 18+

##Usage

Add to your root build.gradle:
```Groovy
allprojects {
	repositories {
	...
	maven { url "https://jitpack.io" }
	}
}
```

Add the dependency:
```Groovy
dependencies {
	  compile 'com.github.Yalantis:SearchFilter:v1.0.4'
}
```

## How to use this library

Firstly you need to place `Filter` above your `RecyclerView` in the layout file

```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.design.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <android.support.design.widget.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <android.support.v7.widget.Toolbar
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="@color/colorPrimary"
            android:elevation="4dp"
            android:paddingRight="16dp">

            <RelativeLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <android.support.v7.widget.AppCompatImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:layout_centerVertical="true"
                    android:src="@drawable/ic_alarm"
                    android:tint="@android:color/white" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_centerInParent="true"
                    android:text="Questions"
                    android:textColor="@android:color/white"
                    android:textSize="20sp" />

                <android.support.v7.widget.AppCompatImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:layout_alignParentRight="true"
                    android:layout_centerVertical="true"
                    android:src="@drawable/ic_search"
                    android:tint="@android:color/white" />

            </RelativeLayout>

        </android.support.v7.widget.Toolbar>
    </android.support.design.widget.AppBarLayout>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#E4E6E3"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/list"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="@dimen/container_height"
            tools:listitem="@layout/item_list" />

        <com.yalantis.filter.widget.Filter
            android:id="@+id/filter"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

    </FrameLayout>
</android.support.design.widget.CoordinatorLayout>

```

After that you need to create a class that extends `FilterAdapter` and to pass your model class as a generic. 
Here you can easily customize the appearance of filter items. For the sample app I created `Tag` model to represent 
the category of a question in the conversation.

```java 

class Adapter extends FilterAdapter<Tag> {

        Adapter(@NotNull List<? extends Tag> items) {
            super(items);
        }

        @NotNull
        @Override
        public FilterItem createView(int position, Tag item) {
            FilterItem filterItem = new FilterItem(ExampleActivity.this);

            filterItem.setStrokeColor(mColors[0]);
            filterItem.setTextColor(mColors[0]);
            filterItem.setCheckedTextColor(ContextCompat.getColor(ExampleActivity.this, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(ExampleActivity.this, android.R.color.white));
            filterItem.setCheckedColor(mColors[position]);
            filterItem.setText(item.getText());
            filterItem.deselect();

            return filterItem;
        }
    }

```

To receive all the events from the `Filter` such as selection or deselection of a filter item it's necessary 
to add a `FilterListener` with the same model class passed as a generic

```java

private FilterListener<Tag> mListener = new FilterListener<Tag>() {

        @Override
        public void onFiltersSelected(@NotNull ArrayList<Tag> filters) {
            
        }

        @Override
        public void onNothingSelected() {

        }

        @Override
        public void onFilterSelected(Tag item) {

        }

        @Override
        public void onFilterDeselected(Tag item) {

        }

    };

```

Basically `Filter` setup looks like that 

```java

 mFilter = (Filter<Tag>) findViewById(R.id.filter);
 mFilter.setAdapter(new Adapter(getTags()));
 mFilter.setListener(this);

 //the text to show when there's no selected items
 mFilter.setNoSelectedItemText(getString(R.string.str_all_selected));
 mFilter.build();

```

For more usage examples please review sample app

## Let us know!

We’d be really happy if you sent us links to your projects where you use our component. Just send an email to github@yalantis.com And do let us know if you have any questions or suggestion regarding the animation. 

P.S. We’re going to publish more awesomeness wrapped in code and a tutorial on how to make UI for iOS (Android) better than better. Stay tuned!

## License

	The MIT License (MIT)

	Copyright © 2016 Yalantis, https://yalantis.com

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
