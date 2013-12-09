package restx.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

/**
 * User: xavierhanin
 * Date: 3/19/13
 * Time: 3:06 PM
 */
public interface UUIDGenerator {
    String doGenerate();
}
