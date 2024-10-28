package phanastrae.ywsanf.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.function.Function;

public class CodecUtil {

    public static final Codec<Float> NON_NEGATIVE_FLOAT = floatRangeInclusiveWithMessage(
            0.0F, Float.MAX_VALUE, f -> "Value must be non-negative: " + f
    );

    private static Codec<Float> floatRangeInclusiveWithMessage(float min, float max, Function<Float, String> errorMessage) {
        return Codec.FLOAT
                .validate(
                        f -> f.compareTo(min) >= 0 && f.compareTo(max) <= 0
                                ? DataResult.success(f)
                                : DataResult.error(() -> errorMessage.apply(f))
                );
    }
}
