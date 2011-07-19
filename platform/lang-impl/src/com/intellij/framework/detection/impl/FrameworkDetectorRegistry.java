/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.framework.detection.impl;

import com.intellij.framework.detection.FrameworkDetector;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author nik
 */
public abstract class FrameworkDetectorRegistry {
  public static FrameworkDetectorRegistry getInstance() {
    return ServiceManager.getService(FrameworkDetectorRegistry.class);
  }

  public abstract int getDetectorsVersion();

  public abstract int getDetectorId(@NotNull FrameworkDetector detector);

  @Nullable
  public abstract FrameworkDetector getDetectorById(int id);
}
