/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package simoneb.targetprocess;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.issueTracker.AbstractIssueProvider;
import jetbrains.buildServer.issueTracker.IssueFetcher;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author Oleg Rybak <oleg.rybak@jetbrains.com>
 */
public class TargetProcessIssueProvider extends AbstractIssueProvider {

  @NotNull
  public static final String TYPE = "targetprocess";

  @NotNull
  public static final String DISPLAY_NAME = "TargetProcess";

  private static final Logger LOG = Logger.getInstance(TargetProcessIssueProvider.class.getName());

  public TargetProcessIssueProvider(@NotNull final IssueFetcher fetcher) {
    super(TYPE, fetcher);
  }

  @Override
  @NotNull
  protected String extractId(@NotNull String match) {
    final Matcher matcher = myPattern.matcher(match);
    if (matcher.find() && matcher.groupCount() >= 1) {
      String group = matcher.group(1);
      if (group == null) {
        LOG.warn("Failed to extract the issue id. Regex=" + myPattern + ", input=" + match);
        return super.extractId(match);
      }
      return group;
    } else {
      return super.extractId(match);
    }
  }

  @NotNull
  @Override
  public PropertiesProcessor getPropertiesProcessor() {
    final PropertiesProcessor superProcessor = super.getPropertiesProcessor();
    return new PropertiesProcessor() {

      private void checkNotEmpty(Map<String, String> properties,
                                 @NotNull Collection<InvalidProperty> result,
                                 @NotNull final String fieldId,
                                 @NotNull final String fieldName) {
        final String value = properties.get(fieldId);
        if (StringUtil.isEmptyOrSpaces(value)) {
          result.add(new InvalidProperty(fieldId, fieldName + " must not be empty"));
        }
      }

      public Collection<InvalidProperty> process(Map<String, String> properties) {
        final Collection<InvalidProperty> result = superProcessor.process(properties);
        checkNotEmpty(properties, result, "host", "Server URL");
        checkNotEmpty(properties, result, "username", "Username");
        checkNotEmpty(properties, result, "secure:password", "Password");
        return result;
      }
    };
  }
}
