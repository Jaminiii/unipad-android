package com.kimjisub.launchpad.adapter;

import android.support.v7.widget.RecyclerView;

import com.kimjisub.design.PackViewSimple;

public class UnipackHolder extends RecyclerView.ViewHolder {
	PackViewSimple packViewSimple;
	int position = -1;

	public UnipackHolder(PackViewSimple view) {
		super(view);
		packViewSimple = view;
	}
}