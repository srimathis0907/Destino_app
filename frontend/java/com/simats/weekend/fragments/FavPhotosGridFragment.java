package com.simats.weekend.fragments;

public class FavPhotosGridFragment extends BaseFavMediaFragment {
    @Override
    protected boolean isCorrectFileType(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".jpeg");
    }

    @Override
    protected String getShareType() {
        return "image/*";
    }
}