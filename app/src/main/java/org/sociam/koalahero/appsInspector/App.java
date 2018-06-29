package org.sociam.koalahero.appsInspector;

import android.arch.core.util.Function;
import android.content.Context;

import org.sociam.koalahero.csm.CSMAPI;
import org.sociam.koalahero.csm.CSMAppInfo;
import org.sociam.koalahero.koala.KoalaData.InteractionRequestDetails;
import org.sociam.koalahero.trackerMapper.TrackerMapperAPI;
import org.sociam.koalahero.trackerMapper.TrackerMapperCompany;
import org.sociam.koalahero.xray.XRayAppInfo;
import java.util.HashMap;

public class App implements Comparable<App>{

    private XRayAppInfo xRayAppInfo;
    private boolean selectedToDisplay;
    private boolean inTop10;

    // Day, Week, and Monthly Usage times for this app.
    private java.util.Map<Interval,Long> usageTimes;

    // Mapped Company Hostname Information
    public HashMap<String, TrackerMapperCompany> companies;

    // Country Code counts.
    public HashMap<String, Integer> localeCounts;

    // Information scraped from Common Sense Media
    private CSMAppInfo csmAppInfo;

    public App(XRayAppInfo xRayAppInfo, Context context){
        this.xRayAppInfo = xRayAppInfo;
        this.selectedToDisplay = false;
        this.inTop10 = false;

        usageTimes = new java.util.HashMap<Interval,Long>();

        usageTimes.put(Interval.DAY,AppsInspector.calculateAppTimeUsage(Interval.DAY, this.xRayAppInfo.app, context));
        usageTimes.put(Interval.WEEK,AppsInspector.calculateAppTimeUsage(Interval.WEEK, this.xRayAppInfo.app, context));
        usageTimes.put(Interval.MONTH,AppsInspector.calculateAppTimeUsage(Interval.MONTH, this.xRayAppInfo.app, context));
        this.companies = new HashMap<>();

        this.csmAppInfo = new CSMAppInfo();
        this.mapXRayHostNames(context);

        this.localeCounts = new HashMap<>();

    }



    public void mapXRayHostNames(final Context context) {
        TrackerMapperAPI TMAPI = TrackerMapperAPI.getInstance(context);
        TMAPI.executeTrackerMapperRequest(
                new Function<TrackerMapperCompany, Void>() {
                    @Override
                    public Void apply(TrackerMapperCompany input) {
                        if(!companies.containsKey(input.companyName)) {
                            companies.put(input.companyName, input);
                        }
                        companies.get(input.companyName).occurrences += 1;


                        if(!localeCounts.containsKey(input.locale)) {
                            localeCounts.put(input.locale, 0);
                        }
                        localeCounts.put(input.locale, localeCounts.get(input.locale) + 1);


                        return null;
                    }
                },
            xRayAppInfo.hosts.toArray(new String[xRayAppInfo.hosts.size()])
        );
    }

    public CSMAppInfo getCsmAppInfo() {
        return csmAppInfo;
    }

    public void setCsmAppInfo(CSMAppInfo info) {
        this.csmAppInfo = info;
    }

    public String getPackageName(){
        return xRayAppInfo.app;
    }

    public boolean isSelectedToDisplay(){
        return selectedToDisplay;
    }

    public void setIsSelectedToDisplay( boolean display){
        this.selectedToDisplay = display;
    }

    public void setSelectedToDisplay( boolean selectedToDisplay){
        this.selectedToDisplay = selectedToDisplay;
    }

    public XRayAppInfo getxRayAppInfo() {
        return xRayAppInfo;
    }

    public boolean isInTop10() {
        return inTop10;
    }

    public void setInTop10(boolean inTop10) {
        this.inTop10 = inTop10;
    }


    public long getDayUsage() {
        return usageTimes.get(Interval.DAY);
    }

    public void setDayUsage(long dayUsage) {
        usageTimes.put(Interval.DAY,dayUsage);
    }

    public long getWeekUsage() {
        return usageTimes.get(Interval.WEEK);
    }

    public void setWeekUsage(long weekUsage) {
        usageTimes.put(Interval.WEEK,weekUsage);
    }

    public long getMonthUsage() {
        return usageTimes.get(Interval.MONTH);
    }

    public void setMonthUsage(long monthUsage) {
        usageTimes.put(Interval.MONTH,monthUsage);
    }

    public long getUsage(Interval inter){
        return usageTimes.get(inter);
    }

    private Interval sortBy;
    public void setSortMode( Interval sort ){
        sortBy = sort;
    }

    @Override
    public int compareTo(App other){

        // Default done by week
        if( usageTimes.get(sortBy) < other.getUsage(sortBy)) return -1;
        if( usageTimes.get(sortBy) > other.getUsage(sortBy)) return 1;
        else return 0;

    }
}
