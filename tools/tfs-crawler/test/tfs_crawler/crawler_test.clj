(ns tfs-crawler.crawler-test
  (:require [midje.sweet :refer :all]
            [cheshire.core :refer :all]
            [tfs-crawler.crawler :as crawler]
            [clj-http.client :as http]))

