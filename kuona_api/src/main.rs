#![feature(rustc_private, plugin)]
#![plugin(rocket_codegen)]

extern crate rocket;
#[macro_use]
extern crate rocket_contrib;

extern crate elastic;
use elastic::prelude::*;

extern crate env_logger;
#[macro_use]
extern crate serde_json;

use std::error::Error;

use serde_json::Value;

fn run() -> Result<(), Box<Error>> {
  // A reqwest HTTP client and default parameters.
  // The `params` includes the base node url (http://localhost:9200).
  let client = SyncClientBuilder::new().build()?;

  // Send the request and process the response.
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

  // Iterate through the hits in the response.
  for hit in res.hits() {
    println!("{:?}", hit);
  }

  println!("{:?}", res);

  Ok(())
}

mod search;

fn run2() -> Result<(), Box<Error>> {
  let results = search::search()?;

  // Iterate through the hits in the response.
  for hit in results {
    println!("{:?}", hit);
  }

  println!("{:?}", results);

  println!("Done with search results");
  Ok(())
}

//fn main() {
//  env_logger::init();
//
//  run2().unwrap();
//  run().unwrap();
//}


#[get("/")]
fn searh_handler() -> Json<Value> {
  let results = search::search()?;
  Json(json!(results))
}


fn main() {
    rocket::ignite()
        .mount("/api/search", routes![search_hendler])
        .launch();
}

