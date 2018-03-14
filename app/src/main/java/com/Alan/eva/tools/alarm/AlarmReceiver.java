package com.Alan.eva.tools.alarm;

import com.Alan.eva.ui.activity.AlarmDealActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			// Intent service = new Intent(context, AlarmService.class);
			// context.startService(service);
			AlarmClockManager.setNextAlarm(context);
		} else {
			Intent deal = new Intent(context, AlarmDealActivity.class);
			deal.putExtra(Alarm.Columns._ID, intent.getIntExtra(Alarm.Columns._ID, 0));
			deal.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(deal);
		}
	}
}
