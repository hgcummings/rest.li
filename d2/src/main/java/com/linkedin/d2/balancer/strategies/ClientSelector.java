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
package com.linkedin.d2.balancer.strategies;

import com.linkedin.d2.balancer.clients.TrackerClient;
import com.linkedin.d2.balancer.util.hashing.HashFunction;
import com.linkedin.d2.balancer.util.hashing.Ring;
import com.linkedin.r2.message.Request;
import com.linkedin.r2.message.RequestContext;
import java.net.URI;
import java.util.Map;
import javax.annotation.Nullable;


/**
 * The selector that selects a {@link TrackerClient} from the ring to route the request to
 */
public interface ClientSelector
{
  /**
   * @param request
   * @param requestContext
   * @param ring
   * @param trackerClients
   * @return
   */
  @Nullable
  TrackerClient getTrackerClient(Request request, RequestContext requestContext, Ring<URI> ring,
      Map<URI, TrackerClient> trackerClients);

  HashFunction<Request> getRequestHashFunction();
}
