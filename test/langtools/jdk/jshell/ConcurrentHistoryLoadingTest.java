/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import jdk.jshell.tool.JavaShellToolBuilder;
import org.junit.jupiter.api.Test;

/*
 * @test
 * @bug 8347418
 * @summary Verify that launching JShell concurrently doesn't lead to a
 *          NullPointerException when JShell tries to load the history
 * @run junit ConcurrentHistoryLoadingTest
 */
public class ConcurrentHistoryLoadingTest {

    @Test
    public void testConcurrentHistoryLoadingAndStoring() throws Throwable {
        final int numTasks = 30;
        final List<Future<Void>> results = new ArrayList<>();
        try (final ExecutorService executor = Executors.newCachedThreadPool()) {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100));
            // run the concurrent tasks
            for (int i = 0; i < numTasks; i++) {
                final Future<Void> f = executor.submit(() -> {
                    // prepare input with rundom number of lines
                    StringBuilder input = new StringBuilder();
                    int max = ThreadLocalRandom.current().nextInt(8) + 1;
                    for (int j = 1; j < max; j++) {
                        input.append("int x").append(j).append(" = 42\n");
                    }
                    // launch jshell using shared preferences
                    JavaShellToolBuilder
                            .builder()
                            .in(new ByteArrayInputStream(input.toString().getBytes()), null)
                            .start();
                    return null;
                });
                results.add(f);
            }
            // wait for the tasks to complete
            for (final Future<Void> f : results) {
                f.get();
            }
        }
    }
}
