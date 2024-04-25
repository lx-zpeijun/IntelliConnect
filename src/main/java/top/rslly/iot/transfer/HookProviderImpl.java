/**
 * Copyright © 2023-2030 The ruanrongman Authors
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.rslly.iot.transfer;

import com.alibaba.fastjson.JSON;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import top.rslly.iot.models.DataEntity;
import top.rslly.iot.services.DataServiceImpl;
import top.rslly.iot.services.ProductDataServiceImpl;
import top.rslly.iot.services.ProductDeviceServiceImpl;
import top.rslly.iot.utility.exhook.*;

import java.util.Arrays;
import java.util.UUID;

@GrpcService
@Slf4j
public class HookProviderImpl extends HookProviderGrpc.HookProviderImplBase {

  @Autowired
  private ProductDeviceServiceImpl productDeviceService;
  @Autowired
  private DealThingsModel dealThingsModel;


  @Override
  public void onProviderLoaded(ProviderLoadedRequest request,
      StreamObserver<LoadedResponse> responseObserver) {
    // DEBUG("onProviderLoaded", request);
    HookSpec[] specs = {

        HookSpec.newBuilder().setName("client.connected").build(),
        HookSpec.newBuilder().setName("client.disconnected").build(),
        HookSpec.newBuilder().setName("message.delivered").build(),
        // HookSpec.newBuilder().setName("message.publish").build(),

    };
    LoadedResponse reply = LoadedResponse.newBuilder().addAllHooks(Arrays.asList(specs)).build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }


  @Override
  public void onClientConnected(ClientConnectedRequest request,
      StreamObserver<EmptySuccess> responseObserver) {
    log.info("onClientConnected {}", request.getClientinfo().getClientid());
    var deviceEntityList =
        productDeviceService.findAllByClientId(request.getClientinfo().getClientid());
    if (!deviceEntityList.isEmpty()) {
      productDeviceService.updateOnlineByClientId("connected",
          deviceEntityList.get(0).getClientId());
    }

    EmptySuccess reply = EmptySuccess.newBuilder().build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }

  @Override
  public void onClientDisconnected(ClientDisconnectedRequest request,
      StreamObserver<EmptySuccess> responseObserver) {
    log.info("onClientDisconnected {}", request.getClientinfo().getClientid());
    var deviceEntityList =
        productDeviceService.findAllByClientId(request.getClientinfo().getClientid());
    if (!deviceEntityList.isEmpty()) {
      productDeviceService.updateOnlineByClientId("disconnected",
          deviceEntityList.get(0).getClientId());
    }
    EmptySuccess reply = EmptySuccess.newBuilder().build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }

  @Override
  public void onMessageDelivered(MessageDeliveredRequest request,
      StreamObserver<EmptySuccess> responseObserver) {
    dealThingsModel.deal(request.getClientinfo().getClientid(), request.getMessage().getTopic(),
        request.getMessage().getPayload().toStringUtf8());
    EmptySuccess reply = EmptySuccess.newBuilder().build();
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }


}
