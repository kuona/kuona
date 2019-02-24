(ns kuona-api.core.collector.adr-collector-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [kuona-api.core.collector.adr-collector :as adr]
            [clojure.java.io :as io]
            [kuona-api.core.util :as util]))


(facts "about adr collection"
       (fact "adr directory specified in .adr-dir file"
             (adr/directory "..") => "docs/architecture/decisions"

             )
       (fact "record lists files found in decision directory"
             (let [decisions (adr/decisions ".." (adr/directory ".."))]
               (count (-> decisions :adrs)) => 2))

       (fact "adr file types"
             (adr/file-type "foo.md") => :markdown
             (adr/file-type "foo.foo") => :text
             (adr/file-type "foo.adoc") => :asciidoc
             )

       (fact "reading markdown decision"
             (let [adr-file (io/file "../docs/architecture/decisions/0001-record-architecture-decisions.md")
                   adr      (adr/read-decision (util/canonical-path-from-string "..") adr-file)]
               (-> adr :file :name) => "0001-record-architecture-decisions.md"
               (-> adr :file :type) => :markdown
               (-> adr :file :path) => "/docs/architecture/decisions/0001-record-architecture-decisions.md"
               (-> adr :contents) => (slurp adr-file)
               )))
