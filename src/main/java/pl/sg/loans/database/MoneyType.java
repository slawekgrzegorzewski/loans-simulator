package pl.sg.loans.database;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.spi.ValueAccess;
import org.hibernate.usertype.CompositeUserType;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class MoneyType implements CompositeUserType<Money> {

    public static class MoneyMapper {
        BigDecimal amount;
        String currency;
    }

    public MoneyType() {
    }

    @Override
    public Object getPropertyValue(Money component, int property) throws HibernateException {
        return switch (property) {
            case 0 -> component.getAmount();
            case 1 -> component.getCurrencyUnit().getCode();
            default -> null;
        };
    }

    @Override
    public Money instantiate(ValueAccess values, SessionFactoryImplementor sessionFactory) {
        BigDecimal amount = values.getValue(0, BigDecimal.class);
        CurrencyUnit currency = CurrencyUnit.of(values.getValue(1, String.class));
        return Money.of(currency, amount, RoundingMode.HALF_EVEN);
    }

    @Override
    public Class<MoneyMapper> embeddable() {
        return MoneyMapper.class;
    }

    @Override
    public Class<Money> returnedClass() {
        return Money.class;
    }

    @Override
    public boolean equals(Money x, Money y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Money x) {
        return x.hashCode();
    }

    @Override
    public Money deepCopy(Money value) {
        return Money.of(value);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Money value) {
        return (Serializable) value;
    }

    @Override
    public Money assemble(Serializable cached, Object owner) {
        return (Money) cached;
    }

    @Override
    public Money replace(Money detached, Money managed, Object owner) {
        return detached;
    }
}