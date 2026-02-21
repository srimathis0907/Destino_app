package com.simats.weekend.fragments;

public class FavVideosGridFragment extends BaseFavMediaFragment {
    @Override
    protected boolean isCorrectFileType(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".mp4") || lowerCaseName.endsWith(".mkv") || lowerCaseName.endsWith(".webm");
    }

    @Override
    protected String getShareType() {
        return "video/*";
    }
}