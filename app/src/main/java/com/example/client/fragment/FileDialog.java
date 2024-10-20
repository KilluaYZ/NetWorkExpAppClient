package com.example.client.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.client.R;
import com.example.client.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FileDialog extends DialogFragment {
	private MaterialAlertDialogBuilder builder;
	private MainViewModel mViewModel;
	private ProgressBar progressBar;
	private TextView tvSum;
	private TextView tvCount;
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		// 创建并返回对话框
		initViews();
		if (getActivity() != null) {
			mViewModel = new MainViewModel(getActivity().getApplication());
		}
		return builder.create();
	}

	private void initViews() {
		// 创建LayoutInflater对象
		LayoutInflater inflater = requireActivity().getLayoutInflater();

		// 加载自定义布局
		View dialogView = inflater.inflate(R.layout.dialog, null);
		builder = new MaterialAlertDialogBuilder(requireContext());
		// 设置自定义布局
		builder.setView(dialogView);

		progressBar = dialogView.findViewById(R.id.progressBar);
		tvSum = dialogView.findViewById(R.id.tvSum);
		tvCount = dialogView.findViewById(R.id.tvCount);
	}


}
