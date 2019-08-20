package com.kimjisub.launchpad.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.kimjisub.design.PackViewSimple;
import com.kimjisub.launchpad.R;
import com.kimjisub.launchpad.activity.BaseActivity;

import java.util.ArrayList;

public class UnipackAdapter extends RecyclerView.Adapter<UnipackHolder> {

	BaseActivity context;
	ArrayList<UnipackItem> list;

	public UnipackAdapter(BaseActivity context, ArrayList<UnipackItem> list, EventListener eventListener) {
		this.context = context;
		this.list = list;
		this.eventListener = eventListener;
	}

	@Override
	public UnipackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		PackViewSimple packViewSimple = new PackViewSimple(parent.getContext());
		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		packViewSimple.setLayoutParams(lp);
		UnipackHolder unipackHolder = new UnipackHolder(packViewSimple);

		return unipackHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull UnipackHolder unipackHolder, int position) {
		UnipackItem item = list.get(position);
		PackViewSimple packViewSimple = unipackHolder.packViewSimple;

		// 이전 데이터에 매핑된 뷰를 제거합니다.
		try {
			list.get(unipackHolder.position).packViewSimple = null;
		} catch (Exception ignored) {
		}

		// 새롭게 할당될 데이터에 뷰를 할당하고 홀더에도 해당 포지션을 등록합니다.
		item.packViewSimple = packViewSimple;
		unipackHolder.position = position;

		////////////////////////////////////////////////////////////////////////////////////////////////

		String title = item.unipack.title;
		String subTitle = item.unipack.producerName;

		if (item.unipack.CriticalError) {
			item.flagColor = context.color(R.color.red);
			title = context.lang(R.string.errOccur);
			subTitle = item.path;
		} else
			item.flagColor = context.color(R.color.skyblue);

		if (item.bookmark)
			item.flagColor = context.color(R.color.orange);


		packViewSimple
				.cancelAllAnimation()
				.setFlagColor(item.flagColor)
				.setTitle(title)
				.setSubTitle(subTitle)
				.setOption1(context.lang(R.string.LED_), item.unipack.isKeyLED)
				.setOption2(context.lang(R.string.autoPlay_), item.unipack.isAutoPlay)
				.setOnEventListener(new PackViewSimple.OnEventListener() {
					@Override
					public void onViewClick(PackViewSimple v) {
						eventListener.onViewClick(item, v);
					}

					@Override
					public void onViewLongClick(PackViewSimple v) {
						eventListener.onViewLongClick(item, v);
					}

					@Override
					public void onPlayClick(PackViewSimple v) {
						eventListener.onPlayClick(item, v);
					}
				})
				.setToggle(item.isToggle, context.color(R.color.red), item.flagColor);


		Animation a = AnimationUtils.loadAnimation(context, R.anim.pack_in);
		if (item.isNew)
			a = AnimationUtils.loadAnimation(context, R.anim.pack_new_in);
		item.isNew = false;
		packViewSimple.setAnimation(a);
	}

	@Override
	public int getItemCount() {
		return list.size();
	}


	////////////////////////////////////////////////////////////////////////////////////////////////

	EventListener eventListener;

	public interface EventListener {
		public void onViewClick(UnipackItem item, PackViewSimple v);

		public void onViewLongClick(UnipackItem item, PackViewSimple v);

		public void onPlayClick(UnipackItem item, PackViewSimple v);
	}
}