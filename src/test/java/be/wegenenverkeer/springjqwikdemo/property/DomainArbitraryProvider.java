package be.wegenenverkeer.springjqwikdemo.property;

import be.wegenenverkeer.springjqwikdemo.domain.*;
import java.util.Collections;
import java.util.Set;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

public class DomainArbitraryProvider implements ArbitraryProvider {

  @Override
  public int priority() {
    return 1;
  }

  @Override
  public boolean canProvideFor(TypeUsage targetType) {
    return targetType.isOfType(Frituurbaar.class)
        || targetType.isOfType(Pataten.class)
        || targetType.isOfType(Frikandellen.class)
        || targetType.isOfType(Kroketten.class)
        || targetType.isOfType(Cervela.class)
        || targetType.isOfType(Bereklauw.class)
        || targetType.isOfType(Krokettype.class);
  }

  @Override
  public Set<Arbitrary<?>> provideFor(
      TypeUsage targetType, ArbitraryProvider.SubtypeProvider subtypeProvider) {
    if (targetType.isOfType(Krokettype.class)) {
      return Collections.singleton(DomainArbitraries.krokettypes());
    }
    if (targetType.isOfType(Pataten.class)) {
      return Collections.singleton(DomainArbitraries.pataten());
    }
    if (targetType.isOfType(Frikandellen.class)) {
      return Collections.singleton(DomainArbitraries.frikandellen());
    }
    if (targetType.isOfType(Kroketten.class)) {
      return Collections.singleton(DomainArbitraries.kroketten());
    }
    if (targetType.isOfType(Cervela.class)) {
      return Collections.singleton(DomainArbitraries.cervela());
    }
    if (targetType.isOfType(Bereklauw.class)) {
      return Collections.singleton(DomainArbitraries.bereklauw());
    }
    if (targetType.isOfType(Frituurbaar.class)) {
      return Collections.singleton(DomainArbitraries.frituurbaar());
    }
    return Collections.emptySet();
  }
}
