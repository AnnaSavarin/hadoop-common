/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.fs.s3a.s3guard;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.s3a.s3guard.PathMetadata;
import org.apache.hadoop.fs.s3a.S3AFileStatus;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests of {@link DirListingMetadata}.
 */
public class TestDirListingMetadata {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testNullPath() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(notNullValue(String.class));
    new DirListingMetadata(null, null, false);
  }

  @Test
  public void testNullListing() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    assertEquals(path, meta.getPath());
    assertNotNull(meta.getListing());
    assertTrue(meta.getListing().isEmpty());
    assertFalse(meta.isAuthoritative());
  }

  @Test
  public void testEmptyListing() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path,
        Collections.<PathMetadata>emptyList(), false);
    assertEquals(path, meta.getPath());
    assertNotNull(meta.getListing());
    assertTrue(meta.getListing().isEmpty());
    assertFalse(meta.isAuthoritative());
  }

  @Test
  public void testListing() {
    Path path = new Path("/path");
    PathMetadata pathMeta1 = new PathMetadata(
        new S3AFileStatus(true, true, new Path(path, "dir1")));
    PathMetadata pathMeta2 = new PathMetadata(
        new S3AFileStatus(true, false, new Path(path, "dir2")));
    PathMetadata pathMeta3 = new PathMetadata(
        new S3AFileStatus(123, 456, new Path(path, "file1"), 789));
    List<PathMetadata> listing = Arrays.asList(pathMeta1, pathMeta2, pathMeta3);
    DirListingMetadata meta = new DirListingMetadata(path, listing, false);
    assertEquals(path, meta.getPath());
    assertNotNull(meta.getListing());
    assertFalse(meta.getListing().isEmpty());
    assertTrue(meta.getListing().contains(pathMeta1));
    assertTrue(meta.getListing().contains(pathMeta2));
    assertTrue(meta.getListing().contains(pathMeta3));
    assertFalse(meta.isAuthoritative());
  }

  @Test
  public void testListingUnmodifiable() {
    Path path = new Path("/path");
    PathMetadata pathMeta1 = new PathMetadata(
        new S3AFileStatus(true, true, new Path(path, "dir1")));
    PathMetadata pathMeta2 = new PathMetadata(
        new S3AFileStatus(true, false, new Path(path, "dir2")));
    PathMetadata pathMeta3 = new PathMetadata(
        new S3AFileStatus(123, 456, new Path(path, "file1"), 789));
    List<PathMetadata> listing = Arrays.asList(pathMeta1, pathMeta2, pathMeta3);
    DirListingMetadata meta = new DirListingMetadata(path, listing, false);
    assertNotNull(meta.getListing());
    exception.expect(UnsupportedOperationException.class);
    meta.getListing().clear();
  }

  @Test
  public void testAuthoritative() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, true);
    assertEquals(path, meta.getPath());
    assertNotNull(meta.getListing());
    assertTrue(meta.getListing().isEmpty());
    assertTrue(meta.isAuthoritative());
  }

  @Test
  public void testSetAuthoritative() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    assertEquals(path, meta.getPath());
    assertNotNull(meta.getListing());
    assertTrue(meta.getListing().isEmpty());
    assertFalse(meta.isAuthoritative());
    meta.setAuthoritative(true);
    assertTrue(meta.isAuthoritative());
  }

  @Test
  public void testGet() {
    Path path = new Path("/path");
    PathMetadata pathMeta1 = new PathMetadata(
        new S3AFileStatus(true, true, new Path(path, "dir1")));
    PathMetadata pathMeta2 = new PathMetadata(
        new S3AFileStatus(true, false, new Path(path, "dir2")));
    PathMetadata pathMeta3 = new PathMetadata(
        new S3AFileStatus(123, 456, new Path(path, "file1"), 789));
    List<PathMetadata> listing = Arrays.asList(pathMeta1, pathMeta2, pathMeta3);
    DirListingMetadata meta = new DirListingMetadata(path, listing, false);
    assertEquals(path, meta.getPath());
    assertNotNull(meta.getListing());
    assertFalse(meta.getListing().isEmpty());
    assertTrue(meta.getListing().contains(pathMeta1));
    assertTrue(meta.getListing().contains(pathMeta2));
    assertTrue(meta.getListing().contains(pathMeta3));
    assertFalse(meta.isAuthoritative());
    assertEquals(pathMeta1, meta.get(pathMeta1.getFileStatus().getPath()));
    assertEquals(pathMeta2, meta.get(pathMeta2.getFileStatus().getPath()));
    assertEquals(pathMeta3, meta.get(pathMeta3.getFileStatus().getPath()));
    assertNull(meta.get(new Path(path, "notfound")));
  }

  @Test
  public void testGetNull() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(NullPointerException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.get(null);
  }

  @Test
  public void testGetRoot() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.get(new Path("/"));
  }

  @Test
  public void testGetNotChild() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.get(new Path("/different/ancestor"));
  }

  @Test
  public void testPut() {
    Path path = new Path("/path");
    PathMetadata pathMeta1 = new PathMetadata(
        new S3AFileStatus(true, true, new Path(path, "dir1")));
    PathMetadata pathMeta2 = new PathMetadata(
        new S3AFileStatus(true, false, new Path(path, "dir2")));
    PathMetadata pathMeta3 = new PathMetadata(
        new S3AFileStatus(123, 456, new Path(path, "file1"), 789));
    List<PathMetadata> listing = Arrays.asList(pathMeta1, pathMeta2, pathMeta3);
    DirListingMetadata meta = new DirListingMetadata(path, listing, false);
    assertEquals(path, meta.getPath());
    assertNotNull(meta.getListing());
    assertFalse(meta.getListing().isEmpty());
    assertTrue(meta.getListing().contains(pathMeta1));
    assertTrue(meta.getListing().contains(pathMeta2));
    assertTrue(meta.getListing().contains(pathMeta3));
    assertFalse(meta.isAuthoritative());
    PathMetadata pathMeta4 = new PathMetadata(
        new S3AFileStatus(true, true, new Path(path, "dir3")));
    meta.put(pathMeta4.getFileStatus());
    assertTrue(meta.getListing().contains(pathMeta4));
    assertEquals(pathMeta4, meta.get(pathMeta4.getFileStatus().getPath()));
  }

  @Test
  public void testPutNull() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(NullPointerException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.put(null);
  }

  @Test
  public void testPutNullPath() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(NullPointerException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.put(new S3AFileStatus(true, true, null));
  }

  @Test
  public void testPutRoot() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.put(new S3AFileStatus(true, true, new Path("/")));
  }

  @Test
  public void testPutNotChild() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.put(new S3AFileStatus(true, true, new Path("/different/ancestor")));
  }

  @Test
  public void testRemove() {
    Path path = new Path("/path");
    PathMetadata pathMeta1 = new PathMetadata(
        new S3AFileStatus(true, true, new Path(path, "dir1")));
    PathMetadata pathMeta2 = new PathMetadata(
        new S3AFileStatus(true, false, new Path(path, "dir2")));
    PathMetadata pathMeta3 = new PathMetadata(
        new S3AFileStatus(123, 456, new Path(path, "file1"), 789));
    List<PathMetadata> listing = Arrays.asList(pathMeta1, pathMeta2, pathMeta3);
    DirListingMetadata meta = new DirListingMetadata(path, listing, false);
    assertEquals(path, meta.getPath());
    assertNotNull(meta.getListing());
    assertFalse(meta.getListing().isEmpty());
    assertTrue(meta.getListing().contains(pathMeta1));
    assertTrue(meta.getListing().contains(pathMeta2));
    assertTrue(meta.getListing().contains(pathMeta3));
    assertFalse(meta.isAuthoritative());
    meta.remove(pathMeta1.getFileStatus().getPath());
    assertFalse(meta.getListing().contains(pathMeta1));
    assertNull(meta.get(pathMeta1.getFileStatus().getPath()));
  }

  @Test
  public void testRemoveNull() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(NullPointerException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.remove(null);
  }

  @Test
  public void testRemoveRoot() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.remove(new Path("/"));
  }

  @Test
  public void testRemoveNotChild() {
    Path path = new Path("/path");
    DirListingMetadata meta = new DirListingMetadata(path, null, false);
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(notNullValue(String.class));
    meta.remove(new Path("/different/ancestor"));
  }
}
