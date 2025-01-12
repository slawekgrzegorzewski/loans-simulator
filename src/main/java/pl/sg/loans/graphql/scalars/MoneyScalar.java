package pl.sg.loans.graphql.scalars;

import com.netflix.graphql.dgs.DgsScalar;
import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.jetbrains.annotations.NotNull;
import org.joda.money.Money;

import java.util.Locale;

@DgsScalar(name = "Money")
public class MoneyScalar implements Coercing<Money, String> {
    @Override
    public String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
        if (dataFetcherResult instanceof Money) {
            return dataFetcherResult.toString();
        } else {
            throw new CoercingSerializeException("Not a valid Money");
        }
    }

    @Override
    public Money parseValue(Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseValueException {
        return Money.parse(input.toString());
    }

    @Override
    public Money parseLiteral(@NotNull Value input, @NotNull CoercedVariables variables, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
            return Money.parse(((StringValue) input).getValue());
        }

        throw new CoercingParseLiteralException("Value is not a valid Money");
    }

    @Override
    public @NotNull Value<StringValue> valueToLiteral(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) {
        return new StringValue(this.serialize(input, graphQLContext, locale));
    }
}
