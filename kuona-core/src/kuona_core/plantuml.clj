(ns kuona-core.plantuml
  (:import (java.io ByteArrayOutputStream File OutputStream BufferedOutputStream FileOutputStream)
           (java.nio.charset Charset)
           (net.sourceforge.plantuml SourceStringReader FileFormatOption FileFormat)))


(def test-src
  (str "@startuml\n" "Bob -> Alice : hello\n" "@enduml\n"))

(defn generate-image [source file-path]
  (let [^SourceStringReader reader (SourceStringReader. source)
        output                     (FileOutputStream. (File. file-path))
        ^OutputStream os           (BufferedOutputStream. output)]
    (.generateImage reader os (FileFormatOption. (FileFormat/SVG)))
    (.close os)))

