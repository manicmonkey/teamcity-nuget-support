/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed;

import jetbrains.buildServer.dataStructures.DecoratingIterator;
import jetbrains.buildServer.dataStructures.Mapper;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntity;
import org.core4j.Func;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.xppimpl.XmlPullXMLFactoryProvider2;

import java.util.Iterator;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 29.01.12 22:42
 */
public abstract class NuGetProducerBase implements NuGetProducer {
  private final InMemoryProducer myProducer;

  public NuGetProducerBase(@NotNull final PackagesIndex index) {
    myProducer = new InMemoryProducer("NuGetGallery");

    //Workaround for Xml generation. Default STAX xml writer
    //used to generate <foo></foo> that is badly parsed in
    //.NET OData WCF client
    XMLFactoryProvider2.setInstance(new XmlPullXMLFactoryProvider2());

    myProducer.register(
            PackageEntity.class,
            "Packages",
            "V2FeedPackage",
            new Func<Iterable<PackageEntity>>() {
              public Iterable<PackageEntity> apply() {
                return new Iterable<PackageEntity>() {
                  public Iterator<PackageEntity> iterator() {
                    return new DecoratingIterator<PackageEntity, NuGetIndexEntry>(
                            index.getNuGetEntries(),
                            new Mapper<NuGetIndexEntry, PackageEntity>() {
                              public PackageEntity mapKey(@NotNull NuGetIndexEntry internal) {
                                return createODataPackageEntry(internal);
                              }
                            });
                  }
                };
              }
            },
            PackageEntity.KeyPropertyNames
    );
  }

  @Nullable
  protected abstract PackageEntity createODataPackageEntry(@NotNull NuGetIndexEntry internal);

  @NotNull
  public ODataProducer getProducer() {
    return myProducer;
  }
}
