package town.amrita.timetable.utils

fun <T> cartesianProduct(lists: List<List<T>>): Sequence<List<T>> = sequence {
  if (lists.isEmpty()) {
    yield(emptyList())
    return@sequence
  }
  if (lists.any { it.isEmpty() }) return@sequence

  val indices = IntArray(lists.size)

  while (true) {
    yield(indices.mapIndexed { i, idx -> lists[i][idx] })

    var pos = lists.size - 1
    while (pos >= 0) {
      indices[pos]++
      if (indices[pos] < lists[pos].size) break
      indices[pos] = 0
      pos--
    }

    if (pos < 0) break
  }
}
