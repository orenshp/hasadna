package(default_visibility = ["//projects/noloan/app:__subpackages__"])

java_library(
  name = 'spam_filter',
  srcs = ["SpamFilter.java"],
  deps =
  [
    "//projects/noloan/app/proto:data_protos_java",
    "@com_google_protobuf//:protobuf_java",
    "//third_party/maven/com/google/guava:guava",
  ],
  resources = ["spam.prototxt"],
)

java_binary(
  name = "spam_filter_tool",
  srcs = ["SpamFilterTool.java"],
  main_class = "projects.noloan.app.filter.SpamFilterTool",
  deps = [
    ":spam_filter",
    "//projects/noloan/app/proto:data_protos_java",    
    "@startup_os//common",
    "@startup_os//common/flags",
    "//third_party/maven/com/google/guava:guava",
    "//third_party/maven/org/slf4j:slf4j_api",
    "//third_party/maven/org/slf4j:slf4j_simple",
    "//third_party/maven/org/apache/commons:commons_csv",
  ]
)
