package com.alorma.github.ui.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by Bernat on 15/07/2014.
 */
public class PaletteUtils {

	public static Palette.Swatch getProfileSwatch(Palette palette) {
		if (palette != null) {
			Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
			Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
			Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
			Palette.Swatch mutedSwatch = palette.getMutedSwatch();
			Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
			Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
			Palette.Swatch item = null;
			if (vibrantSwatch != null) {
				item = vibrantSwatch;
			} else if (darkVibrantSwatch != null) {
				item = darkVibrantSwatch;
			} else if (lightMutedSwatch != null) {
				item = lightMutedSwatch;
			} else if (darkMutedSwatch != null) {
				item = darkMutedSwatch;
			} else if (mutedSwatch != null) {
				item = mutedSwatch;
			} else if (lightVibrantSwatch != null) {
				item = lightVibrantSwatch;
			}
			return item;
		}

		return null;
	}

	public static Palette.Swatch getProfileSwatchDark(Palette palette) {
		if (palette != null) {
			Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
			Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
			Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
			Palette.Swatch item = null;
			if (darkVibrantSwatch != null) {
				item = darkVibrantSwatch;
			} else if (darkMutedSwatch != null) {
				item = darkMutedSwatch;
			} else if (vibrantSwatch != null) {
				item = vibrantSwatch;
			}
			return item;
		}

		return null;
	}

	public void loadImageAndPalette(String url, final PaletteUtilsListener listener) {
		ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				super.onLoadingComplete(imageUri, view, loadedImage);
				Palette.generateAsync(loadedImage, new PaletteListener(loadedImage, listener));
			}
		});
	}

	private class PaletteListener implements Palette.PaletteAsyncListener {

		private Bitmap loadedImage;
		private PaletteUtilsListener listener;

		public PaletteListener(Bitmap loadedImage, PaletteUtilsListener listener) {
			this.loadedImage = loadedImage;
			this.listener = listener;
		}

		@Override
		public void onGenerated(Palette palette) {
			if (listener != null) {
				listener.onImageLoaded(loadedImage, palette);
			}
		}
	}

	public interface PaletteUtilsListener {
		void onImageLoaded(Bitmap loadedImage, Palette palette);
	}
}
