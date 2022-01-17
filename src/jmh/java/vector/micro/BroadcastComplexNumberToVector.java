/*****************************************************************************
 * Copyright (c) 2022, Lev Serebryakov <lev@serebryakov.spb.ru>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ****************************************************************************/

package vector.micro;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
 * This benchmark tries to determine the best way to broadcast complex number
 * represented as a pair of floats to vector of preferable length.
 *
 * Such operation consume two floats and produce SPECIES_PREFERRED vector
 * where these two numbers repeats as many times as possible.
 */
@Fork(2)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 5)
@Threads(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BroadcastComplexNumberToVector {
	private final static int SEED = 42; // Carefully selected, plucked by hands random number

	private final static VectorSpecies<Float> PFS = FloatVector.SPECIES_PREFERRED;
	private final static int EPV = PFS.length();
	private final static VectorSpecies<Float> FS64 = FloatVector.SPECIES_64;

    // [ 0, 1, 0, 1, ...]
	private final static VectorShuffle<Float> SHUFFLE_CS_TO_CV = VectorShuffle.fromOp(PFS, i -> i % 2);
	private final static int[] LOAD_CS_TO_CV_SPREAD = SHUFFLE_CS_TO_CV.toArray();

	private float[] x;

	@Setup(Level.Trial)
	public void Setup() {
		Random r = new Random(SEED);

		x = new float[EPV];
		for (int i = 0; i < x.length; i++)
			x[i] = r.nextFloat() * 2.0f - 1.0f;
	}

	@Benchmark
	public void fromArrayWithIndexMap(Blackhole bh) {
		bh.consume(FloatVector.fromArray(PFS, x, 0, LOAD_CS_TO_CV_SPREAD, 0));
	}

	@Benchmark
	public void load64ReshapeReshuffle(Blackhole bh) {
		bh.consume(FloatVector.fromArray(FS64, x, 0)
            .reinterpretShape(PFS, 0).reinterpretAsFloats()
            .rearrange(SHUFFLE_CS_TO_CV)
        );
	}
}
