package net.maxbraun.mirror;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import net.maxbraun.mirror.Air.AirData;
import net.maxbraun.mirror.Commute.CommuteSummary;
import net.maxbraun.mirror.DataUpdater.UpdateListener;
import net.maxbraun.mirror.Weather.WeatherData;

/**
 * The main {@link Activity} class and entry point into the UI.
 */
public class HomeActivity extends Activity {

  /**
   * The IDs of {@link TextView TextViews} in {@link R.layout#activity_home} which contain the news
   * headlines.
   */
  private static final int[] NEWS_VIEW_IDS = new int[]{
      R.id.news_1,
      R.id.news_2,
  };

  /**
   * The listener used to populate the UI with weather data.
   */
  private final UpdateListener<WeatherData> weatherUpdateListener =
      new UpdateListener<WeatherData>() {
    @Override
    public void onUpdate(WeatherData data) {
      if (data != null) {

        // Populate the current temperature rounded to a whole number.
        String temperature = String.format(Locale.US, "%d°",
            Math.round(getLocalizedTemperature(data.currentTemperature)));
        temperatureView.setText(temperature);

        // Populate the 24-hour forecast summary, but strip any period at the end.
        String summary = util.stripPeriod(data.forecastSummary);
        weatherSummaryView.setText(summary);

        // Populate the precipitation probability as a percentage rounded to a whole number.
        String precipitation =
            String.format(Locale.US, "%d%%", Math.round(100 * data.precipitationProbability));
        precipitationView.setText(precipitation);

        // Populate the icon for the current weather.
        iconView.setImageResource(data.currentIcon);

        // Show all the views.
        temperatureView.setVisibility(View.VISIBLE);
        weatherSummaryView.setVisibility(View.VISIBLE);
        precipitationView.setVisibility(View.VISIBLE);
        iconView.setVisibility(View.VISIBLE);
      } else {

        // Hide everything if there is no data.
        temperatureView.setVisibility(View.GONE);
        weatherSummaryView.setVisibility(View.GONE);
        precipitationView.setVisibility(View.GONE);
        iconView.setVisibility(View.GONE);
      }
    }
  };

  /**
   * The listener used to populate the UI with air quality data.
   */
  private final UpdateListener<AirData> airQualityUpdateListener = new UpdateListener<AirData>() {
    @Override
    public void onUpdate(AirData airData) {
      if (airData != null) {

        // Populate the air quality index number and icon.
      } else {
      }
    }
  };

  /**
   * The listener used to populate the UI with news headlines.
   */
  private final UpdateListener<List<String>> newsUpdateListener =
      new UpdateListener<List<String>>() {
    @Override
    public void onUpdate(List<String> headlines) {

      // Populate the views with as many headlines as we have and hide the others.
      for (int i = 0; i < NEWS_VIEW_IDS.length; i++) {
        if ((headlines != null) && (i < headlines.size())) {
          newsViews[i].setText(headlines.get(i));
          newsViews[i].setVisibility(View.VISIBLE);
        } else {
          newsViews[i].setVisibility(View.GONE);
        }
      }
    }
  };


  /**
   * The listener used to populate the UI with the commute summary.
   */
  private final UpdateListener<CommuteSummary> commuteUpdateListener =
      new UpdateListener<CommuteSummary>() {
        @Override
        public void onUpdate(CommuteSummary summary) {
          if (summary != null) {
            commuteTextView.setText(summary.text);
            commuteTextView.setVisibility(View.VISIBLE);
            travelModeView.setImageDrawable(summary.travelModeIcon);
            travelModeView.setVisibility(View.VISIBLE);
            if (summary.trafficTrendIcon != null) {
              trafficTrendView.setImageDrawable(summary.trafficTrendIcon);
              trafficTrendView.setVisibility(View.VISIBLE);
            } else {
              trafficTrendView.setVisibility(View.GONE);
            }
          } else {
            commuteTextView.setVisibility(View.GONE);
            travelModeView.setVisibility(View.GONE);
            trafficTrendView.setVisibility(View.GONE);
          }
        }
      };

  private TextView temperatureView;
  private TextView weatherSummaryView;
  private TextView precipitationView;
  private ImageView iconView;
  private TextView[] newsViews = new TextView[NEWS_VIEW_IDS.length];
  private TextView commuteTextView;
  private ImageView travelModeView;
  private ImageView trafficTrendView;

  private Weather weather;
  private Air air;
  private News news;
  private Commute commute;
  private Util util;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    temperatureView = (TextView) findViewById(R.id.temperature);
    weatherSummaryView = (TextView) findViewById(R.id.weather_summary);
    precipitationView = (TextView) findViewById(R.id.precipitation);
    iconView = (ImageView) findViewById(R.id.icon);
    for (int i = 0; i < NEWS_VIEW_IDS.length; i++) {
      newsViews[i] = (TextView) findViewById(NEWS_VIEW_IDS[i]);
    }
    commuteTextView = (TextView) findViewById(R.id.commute_text);
    travelModeView = (ImageView) findViewById(R.id.travel_mode);
    trafficTrendView = (ImageView) findViewById(R.id.traffic_trend);

    weather = new Weather(this, weatherUpdateListener);
    air = new Air(this, airQualityUpdateListener);
    news = new News(newsUpdateListener);
    commute = new Commute(this, commuteUpdateListener);
    util = new Util(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    weather.start();
    air.start();
    news.start();
    commute.start();
  }

  @Override
  protected void onStop() {
    weather.stop();
    air.stop();
    news.stop();
    commute.stop();
    super.onStop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    util.hideNavigationBar(temperatureView);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    return util.onKeyUp(keyCode, event);
  }

  /**
   * Converts a temperature in degrees Fahrenheit to degrees Celsius, depending on the
   * {@link Locale}.
   */
  private double getLocalizedTemperature(double temperatureFahrenheit) {
    // First approximation: Fahrenheit for US and Celsius anywhere else.
    return Locale.US.equals(Locale.getDefault()) ?
        temperatureFahrenheit : (temperatureFahrenheit - 32.0) / 1.8;
  }
}
