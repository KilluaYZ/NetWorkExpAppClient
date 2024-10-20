package com.example.client.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel {
	public final MutableLiveData<Integer> packageSum = new MutableLiveData<>();
	public final MutableLiveData<Integer> packageCount = new MutableLiveData<>();
	public boolean isConnected = false;

	public MainViewModel(@NonNull Application application) {
		super(application);
		packageCount.setValue(0);
		packageSum.setValue(0);
	}
}
