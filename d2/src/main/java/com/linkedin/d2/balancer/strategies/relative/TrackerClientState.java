/*
   Copyright (c) 2020 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.linkedin.d2.balancer.strategies.relative;

import com.linkedin.util.degrader.ErrorType;
import java.util.Map;


/**
 * Keeps the state of each tracker client for a partition
 */
public class TrackerClientState {
  private long _avgLatency;
  private int _callCount;
  private double _errorRate;
  // TODO: update adjusted min call count
  private int _adjustedMinCallCount;
  private double _healthScore;

  public TrackerClientState(long avgLatency, int callCount, Map<ErrorType, Integer> errorTypeCounts, double initialHealthScore)
  {
    _avgLatency = avgLatency;
    _callCount = callCount;
    _errorRate = getErrorRateByType(errorTypeCounts, callCount);
    _healthScore = initialHealthScore;
  }

  public void updateStats(long avgLatency, int callCount, Map<ErrorType, Integer> errorTypeCounts)
  {
    _avgLatency = avgLatency;
    _callCount = callCount;
    _errorRate = getErrorRateByType(errorTypeCounts, callCount);
  }

  public void updateHealthScore(double healthScore)
  {
    _healthScore = healthScore;
  }

  public long getAvgLatency()
  {
    return _avgLatency;
  }

  public int getCallCount()
  {
    return _callCount;
  }

  public double getErrorRate()
  {
    return _errorRate;
  }

  public int getAdjustedMinCallCount()
  {
    return _adjustedMinCallCount;
  }

  public double getHealthScore()
  {
    return _healthScore;
  }

  private double getErrorRateByType(Map<ErrorType, Integer> errorTypeCounts, int callCount)
  {
    Integer connectExceptionCount = errorTypeCounts.getOrDefault(ErrorType.CONNECT_EXCEPTION, 0);
    Integer closedChannelExceptionCount = errorTypeCounts.getOrDefault(ErrorType.CLOSED_CHANNEL_EXCEPTION, 0);
    Integer serverErrorCount = errorTypeCounts.getOrDefault(ErrorType.SERVER_ERROR, 0);
    Integer timeoutExceptionCount = errorTypeCounts.getOrDefault(ErrorType.TIMEOUT_EXCEPTION, 0);
    return _callCount == 0
        ? 0
        : (double) (connectExceptionCount + closedChannelExceptionCount + serverErrorCount + timeoutExceptionCount) / callCount;
  }

  /**
   * Identify if a client is unhealthy
   */
  public static boolean isUnhealthy(TrackerClientState trackerClientState, long clusterAvgLatency, double highThresholdFactor, double highErrorRate)
  {
    return trackerClientState.getCallCount() >= trackerClientState.getAdjustedMinCallCount()
        && (trackerClientState.getAvgLatency() >= clusterAvgLatency * highThresholdFactor
        || trackerClientState.getErrorRate() >= highErrorRate);
  }

  /**
   * Identify if a client is healthy
   */
  public static boolean isHealthy(TrackerClientState trackerClientState, long clusterAvgLatency, double lowThresholdFactor, double lowErrorRate)
  {
    return trackerClientState.getCallCount() >= trackerClientState.getAdjustedMinCallCount()
        && (trackerClientState.getAvgLatency() <= clusterAvgLatency * lowThresholdFactor
        || trackerClientState.getErrorRate() <= lowErrorRate);
  }
}
