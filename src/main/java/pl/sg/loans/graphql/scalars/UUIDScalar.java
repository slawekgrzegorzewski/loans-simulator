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

import java.util.Locale;
import java.util.UUID;

@DgsScalar(name = "UUID")
public class UUIDScalar implements Coercing<UUID, String> {
    @Override
    public String serialize(@NotNull Object dataFetcherResult, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingSerializeException {
        if (dataFetcherResult instanceof UUID) {
            return dataFetcherResult.toString();
        } else {
            throw new CoercingSerializeException("Not a valid DateTime");
        }
    }

    @Override
    public UUID parseValue(Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseValueException {
        return UUID.fromString(input.toString());
    }

    @Override
    public UUID parseLiteral(@NotNull Value input, @NotNull CoercedVariables variables, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
            return UUID.fromString(((StringValue) input).getValue());
        }

        throw new CoercingParseLiteralException("Value is not a valid ISO date time");
    }

    @Override
    public @NotNull Value<StringValue> valueToLiteral(@NotNull Object input, @NotNull GraphQLContext graphQLContext, @NotNull Locale locale) {
        return new StringValue(this.serialize(input, graphQLContext, locale));
    }
}
