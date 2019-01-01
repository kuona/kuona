#![feature(rustc_private)]

extern crate elastic;
//#[macro_use]
//extern crate serde_json;

use std::error::Error;

use elastic::prelude::*;
use serde_json::Value;

pub fn search() -> Result<(elastic::client::responses::search::Hits<_, Value>), Box<Error>> {
  let client = SyncClientBuilder::new().build()?;

  let res = client
    .search::<Value>()
    .index("_all")
    .body(json!({
            "query": {
                "query_string": {
                    "query": "kuona"
                }
            }
        }))
    .send()?;


  Ok(res.hits())
}

